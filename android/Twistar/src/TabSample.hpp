// Tabbed pane project template
#ifndef TabSample_HPP_
#define TabSample_HPP_

#include <QObject>
#include <bb/platform/bbm/Context>
#include <bb/platform/bbm/MessageService>
#include <bb/platform/bbm/UserProfile>
#include <QtCore>
#include <QDeclarativePropertyMap>
#include "TabSample.hpp"

namespace bb { namespace cascades { class Application; }}

/*!
 * @brief Application pane object
 *
 *Use this object to create and init app UI, to create context objects, to register the new meta types etc.
 */
class QNetworkAccessManager;

class TabSample : public QObject
{
    Q_OBJECT
    Q_PROPERTY(int value READ value WRITE setValue NOTIFY valueChanged)

public:
    TabSample(bb::cascades::Application *app);
    Q_INVOKABLE void launchBrowser(QString url);
    Q_INVOKABLE void displayXML(QString str);
    Q_INVOKABLE void startXML(QString str, int depth);
    virtual ~TabSample() {}
    Q_INVOKABLE void inviteUserToDownloadViaBBM();
    Q_INVOKABLE void updatePersonalMessage(const QString &message);

    int value();
    void setValue(int i);
signals:
    void valueChanged(int);

Q_SIGNALS:
        void complete(const QString &info);

private Q_SLOTS:
	void onProcessReply(void);
	void registrationStateUpdated(bb::platform::bbm::RegistrationState::Type state);

private:
    bb::platform::bbm::UserProfile * m_userProfile;
    bb::platform::bbm::Context *m_context;
    bb::platform::bbm::MessageService *m_messageService;
    QString curr_str, searchfile, nonefile;
    int curr_depth;
    QNetworkAccessManager *mgr;
    QDeclarativePropertyMap *qdpm;
    void continueXML(void);
    int m_bbmUpdate;
};

#endif /* TabSample_HPP_ */
